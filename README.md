
---

# GL\*ES: SSRF → EC2 IMDS → Glue exfil → S3 Flag (AWS Wargame)

> **목표**
> 웹 SSRF 취약점으로 EC2 메타데이터(IMDSv1) 자격 증명을 탈취(CR1)하고, 해당 권한으로 **Glue Job**을 악용해 **CloudWatch 로그를 외부로 유출**하여 2차 자격 증명(CR2)을 획득한 뒤, **S3에서 flag**를 회수하는 엔드투엔드 실습형 워게임.

## 시나리오 한눈에 보기

<img width="1561" height="1307" alt="image" src="https://github.com/user-attachments/assets/2c9392a9-6c4f-459f-8ad4-9a0d508dbf84" />


```mermaid
flowchart TD
    A[사용자 URL 입력기(SSRF)] -->|우회 표기법 이용| B[169.254.169.254 IMDSv1]
    B --> C[Role 이름/임시 크리덴셜 획득 (CR1)]
    C --> D[AWS CLI로 Role/Policy 열람]
    D --> E[Glue Job: log-forwarder-job 실행]
    E -->|--target-url=Webhook| F[CloudWatch 로그 외부 전송]
    F --> G[로그 내 Backup Credentials 획득 (CR2)]
    G --> H[S3 권한 확인 및 flag.txt 다운로드]
```

---

## 시나리오 구성 요소

* **웹앱(취약점)**: 외부 URL을 서버 측에서 요청하여 요약해주는 기능 → **SSRF**
* **EC2 메타데이터(IMDSv1)**: `169.254.169.254` (여러 IP 표기법/IPv6-mapped로 우회)
* **역할/정책**

  * Role: `gles-role`
  * Policy: `gless-ec2-policy` (DefaultVersionId: `v7`)
* **Glue**

  * Job: `log-forwarder-job`
  * Script: `s3://gles-glue-script-bucket/forward_logs.py`
  * 인자: `--target-url` (외부로 로그 POST)
* **CloudWatch Logs**

  * Log Group: `/aws/leaky-log-group`
  * Log Stream: `internal-log-stream`
* **SSM**

  * Parameter: `/prod/flag` (권한 포함 예시)
* **S3**

  * Script Bucket: `gles-glue-script-bucket`
  * Flag Bucket: `gles-flag-bucket` (`flag.txt`)

---

## 데모 목표(플래그)

* 최종 목표: `s3://gles-flag-bucket/flag.txt` 다운로드 및 내용 확인



## 채점/플로우 체크리스트

* [ ] SSRF로 IMDS 접근 우회 성공
* [ ] `gles-role` 및 CR1 JSON 파싱 성공
* [ ] `gless-ec2-policy` v7 권한 검토
* [ ] Glue Job을 `--target-url`로 외부 전송 유도
* [ ] 웹훅에서 CR2 확보
* [ ] CR2로 `gles-flag-bucket/flag.txt` 다운로드
* [ ] 플래그 제출

---

## 방어 관점(학습 포인트)

* **웹앱**

  * URL 입력기: 내부 IP/메타데이터/localhost/CloudMap 차단
  * DNS Rebinding/IPv6-mapped/진법 우회 차단, SSRF Proxy/샌드박스
  * 아웃바운드 egress 제한(네트워크 레벨)
* **EC2/IMDS**

  * **IMDSv2 강제**, hop limit 최소화
  * 필요 최소 Role/Policy 원칙 적용
* **Glue/데이터 파이프라인**

  * 잡 인자 화이트리스트/검증(외부 URL 금지)
  * VPC 엔드포인트/아웃바운드 차단, egress control
  * CloudWatch 로그 내 **자격증명/시크릿 노출 금지**
* **IAM**

  * 일시 권한·세분화, CMK로 S3/SSM 접근 제어
  * Access Analyzer/CI 정책 검증, SCP로 위험 행위 차단
* **S3**

  * 버킷 정책 최소화, 퍼블릭 차단, 서버사이드 암호화
* **SSM**

  * Parameter Store/KMS 정책 분리, `GetParameter` 최소화

---




## 라이선스

* MIT (필요 시 수정)

---

## 크레딧

* Scenario design & implementation: @your-id
* Inspired by real-world cloud exfiltration chains (SSRF ↔ IMDS ↔ Data Pipeline Abuse)



