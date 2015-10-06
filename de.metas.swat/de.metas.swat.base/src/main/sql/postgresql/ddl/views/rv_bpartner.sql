CREATE OR REPLACE VIEW rv_bpartner AS 
SELECT bp.ad_client_id, bp.ad_org_id, bp.isactive, bp.created, bp.createdby, bp.updated, bp.updatedby, bp.c_bpartner_id, 
	bp.value, bp.name, bp.name2, bp.description, bp.issummary, bp.c_bp_group_id, bp.isonetime, bp.isprospect, 
	bp.isvendor, bp.iscustomer, bp.isemployee, bp.issalesrep, bp.referenceno, bp.duns, bp.url, bp.ad_language, 
	bp.taxid, bp.istaxexempt, bp.c_invoiceschedule_id, bp.rating, bp.salesvolume, bp.numberemployees, bp.naics, 
	bp.firstsale, bp.acqusitioncost, bp.potentiallifetimevalue, bp.actuallifetimevalue, bp.shareofcustomer, 
	bp.paymentrule, bp.so_creditlimit, bp.so_creditused, 
	bp.so_creditused - bp.so_creditlimit AS so_creditavailable, 
	bp.c_paymentterm_id, bp.m_pricelist_id, bp.m_discountschema_id, bp.c_dunning_id, 
	bp.isdiscountprinted, bp.so_description, bp.poreference, bp.paymentrulepo, bp.po_pricelist_id, 
	bp.po_discountschema_id, bp.po_paymentterm_id, bp.documentcopies, bp.c_greeting_id, bp.invoicerule, bp.deliveryrule, 
	bp.freightcostrule, bp.deliveryviarule, bp.salesrep_id, bp.sendemail, bp.bpartner_parent_id, 
	bp.invoice_printformat_id, bp.socreditstatus, bp.shelflifeminpct, bp.ad_orgbp_id, bp.flatdiscount, 
	bp.totalopenbalance, 
	c.ad_user_id, c.name AS contactname, c.description AS contactdescription, c.email, c.supervisor_id, c.emailuser, 
	c.c_greeting_id AS bpcontactgreeting, c.title, c.comments, c.phone, c.phone2, c.fax, c.birthday, c.ad_orgtrx_id, c.emailverify, c.ldapuser, c.emailverifydate, c.notificationtype, l.c_bpartner_location_id, a.postal, a.city, a.address1, a.address2, a.address3, a.c_region_id, COALESCE(r.name, a.regionname) AS regionname, a.c_country_id, cc.name AS countryname, sp.sponsorno
FROM x_bpartner_cockpit_search_mv bpcs
   LEFT JOIN c_bpartner bp ON bp.c_bpartner_id = bpcs.c_bpartner_id
   LEFT JOIN c_bpartner_location l ON bp.c_bpartner_id = l.c_bpartner_id AND l.isactive = 'Y'::bpchar
   LEFT JOIN ad_user c ON bp.c_bpartner_id = c.c_bpartner_id AND (c.c_bpartner_location_id IS NULL OR c.c_bpartner_location_id = l.c_bpartner_location_id) AND c.isactive = 'Y'::bpchar
   LEFT JOIN c_location a ON l.c_location_id = a.c_location_id
   LEFT JOIN c_region r ON a.c_region_id = r.c_region_id
   JOIN c_country cc ON a.c_country_id = cc.c_country_id
   LEFT JOIN c_sponsor sp ON bp.c_bpartner_id = sp.c_bpartner_id
   LEFT JOIN c_sponsor_salesrep ssr ON sp.c_sponsor_id = ssr.c_sponsor_id;