/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua;

public class pj_stun_resolve_result {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected pj_stun_resolve_result(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(pj_stun_resolve_result obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pjsuaJNI.delete_pj_stun_resolve_result(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setToken(byte[] value) {
    pjsuaJNI.pj_stun_resolve_result_token_set(swigCPtr, this, value);
  }

  public byte[] getToken() {
	return pjsuaJNI.pj_stun_resolve_result_token_get(swigCPtr, this);
}

  public void setStatus(int value) {
    pjsuaJNI.pj_stun_resolve_result_status_set(swigCPtr, this, value);
  }

  public int getStatus() {
    return pjsuaJNI.pj_stun_resolve_result_status_get(swigCPtr, this);
  }

  public void setName(pj_str_t value) {
    pjsuaJNI.pj_stun_resolve_result_name_set(swigCPtr, this, pj_str_t.getCPtr(value), value);
  }

  public pj_str_t getName() {
    long cPtr = pjsuaJNI.pj_stun_resolve_result_name_get(swigCPtr, this);
    return (cPtr == 0) ? null : new pj_str_t(cPtr, false);
  }

  public void setAddr(SWIGTYPE_p_pj_sockaddr value) {
    pjsuaJNI.pj_stun_resolve_result_addr_set(swigCPtr, this, SWIGTYPE_p_pj_sockaddr.getCPtr(value));
  }

  public SWIGTYPE_p_pj_sockaddr getAddr() {
    return new SWIGTYPE_p_pj_sockaddr(pjsuaJNI.pj_stun_resolve_result_addr_get(swigCPtr, this), true);
  }

  public pj_stun_resolve_result() {
    this(pjsuaJNI.new_pj_stun_resolve_result(), true);
  }

}
