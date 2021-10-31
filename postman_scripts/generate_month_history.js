// Add postman env variable 'server' or replace pm.environment.get('server') with an appropriate URL
const Months = {
    JANUARY: new Month("01", 31),
    FEBRUARY: new Month("02", 28),
    MARCH: new Month("03", 31),
    APRIL: new Month("04", 30),
    MAY: new Month("05", 31),
    JUNE: new Month("06", 30),
    JULY: new Month("07", 31),
    AUGUST: new Month("08", 31),
    SEPTEMBER: new Month("09", 30),
    OCTOBER: new Month("10", 31),
    NOVEMBER: new Month("11", 30),
    DECEMBER: new Month("12", 31)
};

function Month(number, daysInMonth) {
    this.number = number
    this.daysInMonth = daysInMonth
}

function sendSaveHistoryRequest(day, month, year, exerciseId, maxExecutionSeconds) {
    day = day < 10 ? "0" + day : day
    pm.sendRequest({
        url: `${pm.environment.get('server')}/api/study-history`,
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        body: {
            mode: 'raw',
            raw: {
                "exerciseId": exerciseId,
                "startTime": `${year}-${month}-${day}T00:00:00.000Z`,
                "endTime": `${year}-${month}-${day}T00:00:00.000Z`,
                "executionSeconds": Math.floor(Math.random() * Math.floor(maxExecutionSeconds)),
                "tasksCount": 1,
                "wrongAnswers": 0,
                "replaysCount": 0
            }
        }
    }, (error, result) => {
        if (error) {
            console.error('error:', error)
        } else if (result.code !== 200) {
            console.error('error:', JSON.parse(result.stream.toString()).errors)
        } else {
            console.log('History ID:', JSON.parse(result.stream.toString()).id)
        }
    })
}

pm. test('Check Status', () => {
    if (pm.response.code !== 200) {
        console.error(`Error code: ${pm.response.code}`)
        return
    }

    let exerciseId = 2
    let month = Months.JANUARY
    let year = "2020"
    let maxExecutionSeconds = 1800

    for (let day = 1; day <= month.daysInMonth; day++) {
        sendSaveHistoryRequest(day, month.number, year, exerciseId, maxExecutionSeconds)
    }
})
