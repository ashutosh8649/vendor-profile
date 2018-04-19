package com.offershopper.vendorprofileservice.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.offershopper.vendorprofileservice.feignproxy.CouponAndFeedbackProxy;
import com.offershopper.vendorprofileservice.feignproxy.OfferProxy;
import com.offershopper.vendorprofileservice.feignproxy.UserProxy;
import com.offershopper.vendorprofileservice.model.AddressBean;
import com.offershopper.vendorprofileservice.model.CouponAndFeedbackBean;
import com.offershopper.vendorprofileservice.model.CouponAndFeedbackModifiedBean;
import com.offershopper.vendorprofileservice.model.OfferBean;
import com.offershopper.vendorprofileservice.model.OfferCouponsAndFeedbacksBean;
import com.offershopper.vendorprofileservice.model.VendorDetailsBean;
import com.offershopper.vendorprofileservice.model.VendorProfileBean;

@RestController
@EnableFeignClients("com.offershopper.vendorprofileservice.controller")
public class VendorProfileController {

  //http://localhost:4501/vendor-profile/paper@gmail.com

  private VendorDetailsBean vendorDetails, vendorDetailsFinalBean;
  private Optional<VendorDetailsBean> vendorDetailsBean;
  private UserProxy userproxy; 
  private OfferProxy offerProxy;
  private CouponAndFeedbackProxy couponAndFeedbackProxy;
  public VendorProfileBean vendorProfileBean;////
  private OfferCouponsAndFeedbacksBean offersAndFeedbackSingleBean;//
  private List<OfferCouponsAndFeedbacksBean> offerCouponsAndFeedbacksBean;
  private OfferBean offerBean;
  private CouponAndFeedbackBean couponAndFeedbackSingleBean;
  private CouponAndFeedbackModifiedBean couponsAndFeedbackModifiedSingleBean;//
  private List<CouponAndFeedbackModifiedBean> couponsAndFeedbackModifiedBeans;
  
  //finds profile of vendor by id
  @GetMapping("/vendor-profile/{email}")
  public VendorProfileBean seeProfile(@PathVariable String email) {
    ResponseEntity<Optional<VendorDetailsBean>> vendorDetails = userproxy.findUserByEmail(email); 
    vendorDetailsBean=vendorDetails.getBody();
    ResponseEntity<List<OfferBean>> responseOfferBeans=offerProxy.getOffers();
    List<OfferBean> offerBeans=responseOfferBeans.getBody();//check use of iterator in list??
    Iterator<OfferBean> it=offerBeans.iterator();
    
    while(it.hasNext()) {
      
      offerBean=it.next();
      
      if(offerBean.getUserId().equals(email)) {
        
        String offerId=offerBean.getOfferId();
       
        ResponseEntity<List<CouponAndFeedbackBean>> couponAndFeedbackBeans=couponAndFeedbackProxy.getFeedback(offerId);
        List<CouponAndFeedbackBean> couponAndFeedbackList=couponAndFeedbackBeans.getBody();
        Iterator<CouponAndFeedbackBean> iterator=couponAndFeedbackList.iterator();
        
        while(iterator.hasNext()) {
          couponAndFeedbackSingleBean=iterator.next();
          couponsAndFeedbackModifiedSingleBean=new CouponAndFeedbackModifiedBean(couponAndFeedbackSingleBean.getCouponId(),couponAndFeedbackSingleBean.getFeedback());
          couponsAndFeedbackModifiedBeans.add(couponsAndFeedbackModifiedSingleBean);
        }
          

       offersAndFeedbackSingleBean = new OfferCouponsAndFeedbacksBean(offerId, offerBean.getOfferTitle(),offerBean.getOfferValidity(),
           offerBean.getDateOfAnnouncement(), offerBean.getOfferDescription(), offerBean.getOriginalPrice(),offerBean.getDiscount(),
           offerBean.getOfferRating(),offerBean.getOfferCategory(), offerBean.getOfferTerms(),couponsAndFeedbackModifiedBeans);
      }
      offerCouponsAndFeedbacksBean.add(offersAndFeedbackSingleBean);
    }
    
    if(vendorDetailsBean.isPresent()) {
      vendorDetailsFinalBean=vendorDetailsBean.get();
      vendorProfileBean= new VendorProfileBean(vendorDetailsFinalBean.getId(), vendorDetailsFinalBean.getFirstName(),vendorDetailsFinalBean.getLastName(), vendorDetailsFinalBean.getEmail(),
          vendorDetailsFinalBean.getShopAddress(), vendorDetailsFinalBean.getVendorMobileNo(),offerCouponsAndFeedbacksBean);  
    }
  
    
    return vendorProfileBean;
  
 }
}
