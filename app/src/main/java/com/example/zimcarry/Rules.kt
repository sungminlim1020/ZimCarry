package com.example.carryon   // ← 프로젝트 패키지명으로 맞춰주세요

/**
 * 규칙 엔진의 결정 결과
 * - tag: 기내 OK / 위탁 권장 / 금지
 * - reason: 짧은 근거 문구 (UI에 그대로 노출)
 */
data class Decision(val tag: Tag, val reason: String) {
    enum class Tag { OK, CHECKED, PROHIBITED }
}

/**
 * 라벨(클래스명) → 반입 규칙 매핑
 * - labels.txt 의 라인과 정확히 일치해야 함
 * - 안전성을 위해 입력 라벨은 trim + lowercase 로 정규화
 */
object Rules {

    /**
     * @param label  TFLite 예측 라벨 (예: "power_bank")
     * @return      규칙 판단 결과
     */
    fun decide(label: String?): Decision {
        val key = label?.trim()?.lowercase().orEmpty()

        return when (key) {
            // 보조배터리
            // 일반적으로 100Wh(= 20,000mAh@5V) 이하 기내 반입 가능, '위탁'은 안전상 금지
            "power_bank" ->
                Decision(
                    Decision.Tag.OK,
                    "보조배터리: 보통 100Wh 이하 기내 반입, 위탁 금지"
                )

            // 액체 용기
            // 100ml 초과 의심 시 기내 반입 불가 → 위탁 권장 (라벨 OCR로 ml 추출 시 더 정확)
            "liquid_container" ->
                Decision(
                    Decision.Tag.CHECKED,
                    "액체 용기: 100ml 초과 의심 시 기내 불가, 위탁 권장"
                )

            // 라이터
            // 항공사/국가별로 예외가 있으나 보수적으로 금지 처리(혹은 엄격 제한) 권장
            "lighter" ->
                Decision(
                    Decision.Tag.PROHIBITED,
                    "라이터: 화기류로 분류되어 반입 제한 또는 금지(노선별 상이)"
                )

            // 칼/가위류
            "knife_scissor" ->
                Decision(
                    Decision.Tag.PROHIBITED,
                    "칼/가위류: 날 붙은 물품은 기내 반입 금지, 위탁도 제한될 수 있음"
                )

            // 스프레이/에어로졸
            "spray_aerosol" ->
                Decision(
                    Decision.Tag.PROHIBITED,
                    "에어로졸: 압축가스 위험물에 해당, 대부분 반입 금지(노선별 상이)"
                )

            // 일반 물품
            "normal_item" ->
                Decision(
                    Decision.Tag.OK,
                    "일반 안전 물건으로 판단"
                )

            // 알 수 없는 라벨 (모델 클래스에 없음/빈 문자열 등)
            else ->
                Decision(
                    Decision.Tag.CHECKED,
                    "분류 불확실: 실제 규정 확인 권장(의심 물품은 위탁/사전 문의)"
                )
        }
    }
}
