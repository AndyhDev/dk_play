package com.dk.play.util;

public class SkuSigData {
	private String data;
	private String sig;
	private String sku;
	
	public SkuSigData(String rawData, String rawSig, String rawSku) {
		data = rawData;
		sig = rawSig;
		sku = rawSku;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getSig() {
		return sig;
	}
	public void setSig(String sig) {
		this.sig = sig;
	}
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	
	
}
