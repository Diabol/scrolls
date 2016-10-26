package se.diabol.scrolls.engine


enum ExitCodes {
    OK(0),
    FAILED_TO_PARSE_OPTIONS(1),
    MISSING_REQUIRED_OPTIONS(2),
    FAILED_TO_INITIALIZE(3),
    RUNTIME_FAILURE(10)

    final int value

    private ExitCodes(final int value) {
        this.value = value
    }
}
