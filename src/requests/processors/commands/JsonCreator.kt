package requests.processors.commands

class JsonCreator {

    fun createSuccessJson(branch: String): String {
        return """
                {
                    "text": "$branch-branch 빌드가 시작됩니다. (이전 빌드는 자동으로 실행종료 됩니다) "
                }
                """
    }

    fun createFailJson(): String {
        return """
                {
                    "text": "bitrise에서 빌드 시작이 실패했습니다."
                }
                """
    }
}