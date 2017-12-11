/**
 * Created by superman on 2017/10/10.
 */
public class Token {
    private TokenType tokenType;
    private String value;
    private int lineNum;
    private int position;

    public Token(TokenType tokenType, String value,int lineNum, int position) {
        this.tokenType = tokenType;
        this.value = value;
        this.lineNum = lineNum;
        this.position = position;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

	@Override
	public String toString() {
		return "Token [tokenType=" + tokenType + ", value=" + value + ", lineNum=" + lineNum + ", position=" + position
				+ "]";
	}
}
