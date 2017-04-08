/**
 *
 */
package com.transyslab.roadnetwork;

/**
 * 编码（id）对象
 *
 * @author YYL 2016-5-24
 */
public class CodedObject {
	// 对象ID
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
	// 对象名字，由子类NamedObject实现赋值
	public String getName() {
		return name_;
	}
	public void setName(String name) {
		name_ = name;
	}
	public boolean needDeepCopy() {
		return false;
	}
	// 对象复制
	public void deepCopy(CodedObject i) {
		code_ = i.getCode();
	}
	// 对象id排序
	public int cmp(CodedObject i) {
		if (code_ < i.getCode())
			return (-1);
		else if (code_ > i.getCode())
			return (1);
		else
			return (0);
	}
	// 识别对象是否相同
	public boolean eq(CodedObject i) {
		return (code_ == i.getCode());
	}
	// 根据id寻找对象
	public int cmp(int c) {
		if (code_ < c)
			return (-1);
		else if (code_ > c)
			return (1);
		else
			return (0);
	}
	// 输出对象id
	public void print() {

	}
	// 是否被选中
	public boolean isSelected(){
		return this.isSelected;
	}
	public void setSelected(final boolean isSelect){
		this.isSelected = isSelect;
	}
}
