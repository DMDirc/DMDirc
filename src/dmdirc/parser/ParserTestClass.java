/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * SVN: $Id$
 */

package dmdirc.parser;

/**
 * Contains Tests for various parts of the parser.
 * 
 * @author            Shane Mc Cormack
 * @version           $Id$
 * @see IRCParser
 */
public class ParserTestClass {
	private int nTotalTests;
	private int nPassedTests;
	private int nFailedTests;
	private boolean bCanOutput = true;
	private class TestResult {
		boolean bResult;
		String sInput;
		String sExpected;
		String sOutput;
	}

	// sExpected is Case Sensitive.
	private TestResult TestClientInfo(String sInput, String sExpected) {
		++nTotalTests;
		ClientInfo cTemp = new ClientInfo(sInput);
		TestResult tResult = new TestResult();
		tResult.sInput = sInput;
		tResult.sExpected = sExpected;
		tResult.sOutput = cTemp.toString();
		tResult.bResult = tResult.sExpected.equals(tResult.sOutput);
		
		if (tResult.bResult) {
			++nPassedTests;
			if (bCanOutput) { System.out.printf("  [PASS]\t\t"); }
		} else {
			++nFailedTests;
			if (bCanOutput) { System.out.printf("  [FAIL]\t\t"); }
		}
		if (bCanOutput) { System.out.printf("[CLIENT] Input: \"%s\" | Expected: \"%s\" | Output: \"%s\"\n",tResult.sInput,tResult.sExpected,tResult.sOutput); }
		cTemp = null;
		
		return tResult;
	}
	
	
	public void RunTests(String args[]) { RunTests(); }
	public void RunTests() {
		if (bCanOutput) { System.out.printf("  ClientInfo Tests Begin\n"); }
		if (bCanOutput) { System.out.printf("  ----------\n"); }
		TestClientInfo(":Nick!ident@host","Nickname: Nick | Ident: ident | Host: host");
		TestClientInfo("Nick!ident@host","Nickname: Nick | Ident: ident | Host: host");
		TestClientInfo(":Nick!ident","Nickname: Nick | Ident: ident | Host: ");
		TestClientInfo("Nick!ident","Nickname: Nick | Ident: ident | Host: ");
		TestClientInfo(":Nick@host","Nickname: Nick | Ident:  | Host: host");
		TestClientInfo("Nick@host","Nickname: Nick | Ident:  | Host: host");
		TestClientInfo(":Nick","Nickname: Nick | Ident:  | Host: ");
		TestClientInfo("Nick","Nickname: Nick | Ident:  | Host: ");
		if (bCanOutput) { System.out.printf("  ----------\n"); }
		if (bCanOutput) { System.out.printf("  ClientInfo Tests Ended\n"); }
	}
	
	public void SetMute (boolean bMuted) { bCanOutput = !bMuted; }
	public boolean GetMute() { return !bCanOutput; }	
	
	public int GetTotalTests() { return nTotalTests; }
	public int GetPassedTests() { return nPassedTests; }
	public int GetFailedTests() { return nFailedTests;	}	
	
	// Method to allow Parser to test itself.
	public boolean SelfTest() {
		boolean oldbCanOutput = bCanOutput;
		bCanOutput = false;
		RunTests();
		bCanOutput = oldbCanOutput;
		if (nTotalTests > 1) {
			return (nTotalTests == nPassedTests);
		} else { return false; }
	}
}