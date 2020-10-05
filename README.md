# MoblieMapProj

## Payment Information Visualization
- 결제정보를 통해 지도에 마커를 찍어주는 안드로이드 앱

• 사용 방법
1. 사용자가 조회하고자 하는 기간을 선택
2. 시작 버튼을 누르면 시간 순서대로 마커생성
3. 마커를 누르면 해당 장소에서의 결제정보 조회 가능

• 동작 과정
1. 선택된 기간의 결제정보를 DB에서 불러옴
2. 결제정보의 주소를 통해 위도, 경도를 구하고 마커옵션 계산
3. 마커옵션을 통해 마커를 화면상에 찍음

## UI 설계

<img width="394" alt="스크린샷 2020-10-05 오후 10 34 32" src="https://user-images.githubusercontent.com/48786000/95086236-348ce780-075b-11eb-8b46-0ffec0e08b0d.png">
<img width="388" alt="스크린샷 2020-10-05 오후 10 35 02" src="https://user-images.githubusercontent.com/48786000/95086252-39519b80-075b-11eb-8f2e-2b535df0cac8.png">

## 비동기 처리
- 앱 동작 과정에서 DB 파싱, API 통신(위도, 경도), 마커옵션 계산은 UI와 관련이 없기 때문에 Background에서 실행
- Coroutine 사용
