import java.time.LocalDateTime;

public class utils {

	public static boolean minutePassed(LocalDateTime start, LocalDateTime finish, long minutes) {
		LocalDateTime finishWithMinuteSubtraction = finish.minusMinutes(minutes);
		return finishWithMinuteSubtraction.isAfter(start);
	}
}
