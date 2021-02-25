package com.baruch.coupons.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.baruch.coupons.datapresentation.dataInterfaces.IPurchaseDataObject;
import com.baruch.coupons.entities.Purchase;

@Repository
public interface IPurchasesRepository extends CrudRepository<Purchase, Long> {
	
	//These methods serves Customer users only.
	
	@Query("select new com.baruch.coupons.datapresentation.purchase.PurchaseBasicData" + 
	"(p.id, p.amount, p.timeStamp, p.coupon.price, p.coupon.title) from Purchase p where p.user.id = ?1")
	public List<IPurchaseDataObject> getAllPurchasesForCustomer(long userID);

	@Query("select new com.baruch.coupons.datapresentation.purchase.PurchaseDataForCustomer(p.amount, p.timeStamp, p.coupon.price, p.coupon.title, p.coupon.company.name) from Purchase p where p.id = ?1")
	public IPurchaseDataObject getPurchaseForCustomer(long purchaseID);

	@Query("select new com.baruch.coupons.datapresentation.purchase.PurchaseBasicData"
			+ "(p.id, p.amount, p.timeStamp, p.coupon.price, p.coupon.title) from Purchase p where p.coupon.company.id = ?1")
	public List<IPurchaseDataObject> getPurchasesByCopmany(long companyID);

	//This method serves only Company users.

	@Query("select new com.baruch.coupons.datapresentation.purchase.PurchaseBasicData"
			+ "(p.id, p.amount, p.timeStamp, p.coupon.price, p.coupon.title) from Purchase p where p.coupon.company.id = ?1")
	public List<IPurchaseDataObject> getAllPurchasesForCompany(long companyID);
	
	//This methods serve Admin users only.

	@Query("select new com.baruch.coupons.datapresentation.purchase.PurchaseDataForAdmin"+
	"(p.id, p.amount, p.coupon.price, p.timeStamp, p.coupon.title, p.coupon.company.name, p.user.userName) from Purchase p where p.id = ?1")
	public IPurchaseDataObject getPurchaseForAdmin(long purchaseID);

	@Query("select new com.baruch.coupons.datapresentation.purchase.PurchaseDataForAdmin"+ 
	"(p.id, p.amount, p.coupon.price, p.timeStamp, p.coupon.title, p.coupon.company.name, p.user.userName) from Purchase p where p.user.id = ?1")
	public List<IPurchaseDataObject> getPurchasesByUserID(long userID);

	@Query("select new com.baruch.coupons.datapresentation.purchase.PurchaseBasicData" + 
	"(p.id, p.amount, p.timeStamp, p.coupon.price, p.coupon.title) from Purchase p")
	public List<IPurchaseDataObject> getAllPurchases();
}
