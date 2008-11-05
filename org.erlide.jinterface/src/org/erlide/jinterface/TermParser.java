package org.erlide.jinterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.erlide.jinterface.TermParser.Token.TokenKind;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

public class TermParser {

	private static Map<String, OtpErlangObject> cache = new HashMap<String, OtpErlangObject>();

	public static OtpErlangObject parse(String s) throws Exception {
		OtpErlangObject value = cache.get(s);
		if (value == null) {
			value = parse(scan(s));
			cache.put(s, value);
		}
		return value;
	}

	private static OtpErlangObject parse(List<Token> tokens) throws Exception {
		if (tokens.size() == 0) {
			return null;
		}
		OtpErlangObject result = null;
		Token t = tokens.remove(0);
		switch (t.kind) {
		case ATOM:
			result = new OtpErlangAtom(t.text);
			break;
		case VARIABLE:
			result = new OtpVariable(t.text);
			break;
		case STRING:
			result = new OtpErlangString(t.text);
			break;
		case INTEGER:
			result = new OtpErlangLong(Long.parseLong(t.text));
			break;
		case PLACEHOLDER:
			result = new OtpPlaceholder(t.text);
			break;
		case TUPLESTART:
			result = parseSequence(tokens, TokenKind.TUPLEEND,
					new Stack<OtpErlangObject>());
			break;
		case TUPLEEND:
			throw new Exception("unexpected " + t.toString());
		case LISTSTART:
			result = parseSequence(tokens, TokenKind.LISTEND,
					new Stack<OtpErlangObject>());
			break;
		case LISTEND:
			throw new Exception("unexpected " + t.toString());
		case COMMA:
			throw new Exception("unexpected " + t.toString());
		case UNKNOWN:
			throw new Exception("unknown token" + t.toString());
		}
		return result;
	}

	private static OtpErlangObject parseSequence(List<Token> tokens,
			TokenKind stop, Stack<OtpErlangObject> stack) throws Exception {
		if (tokens.size() == 0) {
			return null;
		}
		Token t = tokens.get(0);
		if (t.kind == stop) {
			tokens.remove(0);
			if (stop == TokenKind.LISTEND) {
				return new OtpErlangList(stack.toArray(new OtpErlangObject[0]));
			} else if (stop == TokenKind.TUPLEEND) {
				return new OtpErlangTuple(stack.toArray(new OtpErlangObject[0]));
			}
		} else {
			stack.push(parse(tokens));
			if (tokens.get(0).kind == TokenKind.COMMA) {
				tokens.remove(0);
			}
			return parseSequence(tokens, stop, stack);
		}
		return null;
	}

	static class Token {
		static enum TokenKind {
			ATOM, VARIABLE, STRING, INTEGER, PLACEHOLDER, TUPLESTART, TUPLEEND, LISTSTART, LISTEND, COMMA, UNKNOWN;
		}

		TokenKind kind;
		int start;
		int end;
		String text;

		@Override
		public String toString() {
			return "<" + kind.toString() + ": !" + text + "!>";
		}

		public static Token nextToken(String s) {
			if (s == null || s.length() == 0) {
				return null;
			}
			Token result = new Token();
			char c;
			int i = 0;
			do {
				c = s.charAt(i++);
				if (i >= s.length()) {
					return null;
				}
			} while ((c == ' ' || c == '\t' || c == '\n' || c == '\r'));
			i--;

			result.start = i;
			result.end = i;
			if (c <= 'z' && c >= 'a') {
				result.kind = TokenKind.ATOM;
				while (result.end < s.length() && (c >= 'a' && c <= 'z')
						|| (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
						|| c == '_') {
					c = s.charAt(result.end++);
				}
				result.end--;
			} else if (c == '\'') {
				result.kind = TokenKind.ATOM;
				c = s.charAt(++result.end);
				// TODO add escape!
				while (result.end < s.length() && c != '\'') {
					c = s.charAt(result.end++);
				}
			} else if (c == '"') {
				result.kind = TokenKind.STRING;
				c = s.charAt(++result.end);
				// TODO add escape!
				while (result.end < s.length() && c != '"') {
					c = s.charAt(result.end++);
				}
			} else if ((c >= 'A' && c <= 'Z') || c == '_') {
				result.kind = TokenKind.VARIABLE;
				while (result.end < s.length() && (c >= 'a' && c <= 'z')
						|| (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
						|| c == '_') {
					c = s.charAt(result.end++);
				}
				result.end--;
			} else if (c <= '9' && c >= '0') {
				result.kind = TokenKind.INTEGER;
				while (result.end < s.length() && (c >= '0' && c <= '9')) {
					c = s.charAt(result.end++);
				}
				result.end--;
			} else if (c == '~') {
				result.kind = TokenKind.PLACEHOLDER;
				result.end = result.start + 2;
			} else if (c == '{') {
				result.kind = TokenKind.TUPLESTART;
				result.end = result.start + 1;
			} else if (c == '}') {
				result.kind = TokenKind.TUPLEEND;
				result.end = result.start + 1;
			} else if (c == '[') {
				result.kind = TokenKind.LISTSTART;
				result.end = result.start + 1;
			} else if (c == ']') {
				result.kind = TokenKind.LISTEND;
				result.end = result.start + 1;
			} else if (c == ',') {
				result.kind = TokenKind.COMMA;
				result.end = result.start + 1;
			} else {
				result.kind = TokenKind.UNKNOWN;
				result.end = result.start + 1;
			}
			result.text = s.substring(result.start, result.end);
			char ch = result.text.charAt(0);
			if (ch == '"' || ch == '\'') {
				result.text = result.text
						.substring(1, result.text.length() - 1);
			}
			return result;
		}
	}

	private static List<Token> scan(String s) {
		s = s + " ";
		List<Token> result = new ArrayList<Token>();
		Token t = Token.nextToken(s);
		while (t != null) {
			result.add(t);
			s = s.substring(t.end);
			t = Token.nextToken(s);
		}
		return result;
	}

}