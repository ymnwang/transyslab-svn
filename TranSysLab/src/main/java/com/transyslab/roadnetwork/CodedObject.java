/**
 *
 */
package com.transyslab.roadnetwork;

/**
 * ���루id������
 *
 * @author YYL 2016-5-24
 */
public class CodedObject {
	// ����ID
	private int code_;
	private String name_;
	private boolean isSelected;

	public CodedObject() {
		code_ = 0;
		name_ = "Not named";
	}
	public CodedObject(int i) {
		code_ = i;
	}
	public void setCode(int i) {
		code_ = i;
	}
	public int getCode() {
		return code_;
	}
	// �������֣�������NamedObjectʵ�ָ�ֵ
	public String getName() {
		return name_;
	}
	public void setName(String name) {
		name_ = name;
	}
	public boolean needDeepCopy() {
		return false;
	}
	// ������
	public void deepCopy(CodedObject i) {
		code_ = i.getCode();
	}
	// ����id����
	public int cmp(CodedObject i) {
		if (code_ < i.getCode())
			return (-1);
		else if (code_ > i.getCode())
			return (1);
		else
			return (0);
	}
	// ʶ������Ƿ���ͬ
	public boolean eq(CodedObject i) {
		return (code_ == i.getCode());
	}
	// ����idѰ�Ҷ���
	public int cmp(int c) {
		if (code_ < c)
			return (-1);
		else if (code_ > c)
			return (1);
		else
			return (0);
	}
	// �������id
	public void print() {

	}
	// �Ƿ�ѡ��
	public boolean isSelected(){
		return this.isSelected;
	}
	public void setSelected(final boolean isSelect){
		this.isSelected = isSelect;
	}
}
