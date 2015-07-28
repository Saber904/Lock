package com.example.eleven.speechlibrary;

/**
 * 复数类
 */
class Complex {

	private double m_real;
	private double m_image;
	
	public Complex() {
		m_image = 0.0;
		m_real = 0.0;
	}
	
	public Complex(double real, double image) {
		m_real = real;
		m_image = image;
	}
	
	public void setReal(double real) {
		m_real = real;
	}
	
	public void setImage(double image){
		m_image = image;
	}
	
	public double getReal(){
		return m_real;
	}
	
	public double getImage(){
		return m_image;
	}
	
	public static Complex add(Complex lhs, Complex rhs) {
		Complex result = new Complex(lhs.getReal() + rhs.getReal(), lhs.getImage() + rhs.getImage());
		return result;
	}
	
	public static Complex multiply(Complex lhs, Complex rhs) {
		Complex result = new Complex();
		result.setReal(lhs.getReal()*rhs.getReal() - lhs.getImage()*rhs.getImage());
		result.setImage(lhs.getReal()*rhs.getImage() + lhs.getImage()*rhs.getReal());
		return result;
	}
	
	public static Complex minus(Complex lhs, Complex rhs) {
		Complex result = new Complex(lhs.getReal() - rhs.getReal(), lhs.getImage() - rhs.getImage());
		return result;
	}

}
