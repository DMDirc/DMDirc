import java.lang.Thread;
import org.ownage.dmdirc.parser.*;

public class ParserTest {
	public static void main(String args[]) {
		IRCParser ptc = new IRCParser();
		ptc.DoSelfTest(false);
	}
}
