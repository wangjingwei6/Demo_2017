package bean;

public class NoteItemBean {

	public String mTextContent;
	public String mPhotoPath;
	public int mType; // 1是图片
	public int position;


	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getmPhotoPath() {
		return mPhotoPath;
	}

	public void setmPhotoPath(String mPhotoPath) {
		this.mPhotoPath = mPhotoPath;
	}

	public String getmTextContent() {
		return mTextContent;
	}

	public void setmTextContent(String mTextContent) {
		this.mTextContent = mTextContent;
	}

	public int getmType() {
		return mType;
	}

	public void setmType(int mType) {
		this.mType = mType;
	}
	
	@Override
	public String toString() {
		return "NoteItemBean [mTextContent=" + mTextContent + ", mPhotoPath=" + mPhotoPath + "]";
	}

}
