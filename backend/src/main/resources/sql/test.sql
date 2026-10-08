SELECT COUNT(*) FROM products GROUP BY organization_id;

SELECT o.organization_id, o.name, o.manager, o.manager_phone, o.agreement_date, o.agreement_status, COUNT(products.product_id)
 FROM organization as o LEFT JOIN products
  on o.organization_id = products.organization_id
   GROUP BY o.organization_id;


SELECT o.organization_id, COUNT(products.product_id)
 FROM organization as o LEFT JOIN products
  on o.organization_id = products.organization_id
 GROUP BY o.organization_id 
 ORDER BY o.organization_id asc limit 4;




DROP VIEW 

SELECT products.product_id, name, organization_id, auction.auction_id, bid_price, bid_time FROM products
 left join auction on products.product_id = auction.product_id
 left join bid on auction.auction_id = bid.auction_id
 WHERE organization_id = 1;

SELECT 
    p.product_id,
    p.name,
    p.start_price,
    b.bid_price,
    a.auction_status,
    p.created_at
FROM products p

LEFT JOIN auction a 
    ON p.product_id = a.product_id

LEFT JOIN bid b 
    ON a.auction_id = b.auction_id
    AND b.bid_time = (
        SELECT MAX(b2.bid_time)
        FROM bid b2
        WHERE b2.auction_id = a.auction_id
    )

WHERE p.organization_id = 1;




 left join bid on auction.auction_id = bid.auction_id
 WHERE organization_id = 1
 GROUP BY products.product_id, auction.auction_id, bid_price, bid_time
 SELECT product_id from o_products GROUP BY product_id;

 ORDER BY bid_time;

select * from bid where auction_id = 1;



SELECT category.category_id, category.name, COUNT(product_id) as product_count FROM products RIGHT JOIN category on products.category_id = category.category_id GROUP BY category_id;
select * from category;

select * from products;


SELECT o.organization_id, o.name, o.manager, o.manager_phone, o.agreement_date, o.agreement_status, COUNT(products.product_id)
    FROM organization as o LEFT JOIN products on o.organization_id = products.organization_id
    WHERE o.name = '햇살공방' AND o.agreement_status = TRUE
    GROUP BY o.organization_id

SELECT * FROM auction LEFT JOIN products on auction.product_id = products.product_id WHERE auction_status = "진행";
SELECT products.name, COALESCE(MAX(bid.bid_price), products.start_price) AS currentPrice FROM auction LEFT JOIN products on products.product_id = auction.product_id LEFT JOIN bid on bid.auction_id = auction.auction_id WHERE auction_status = "진행" GROUP BY products.product_id;


SELECT a.auction_id, pd.name, o.name, m.name, py.payment_price, py.created_at, py.payment_status FROM payment py LEFT JOIN auction a on py.auction_id = a.auction_id LEFT JOIN products pd on pd.product_id = a.auction_id LEFT JOIN organization o on o.organization_id = pd.organization_id LEFT JOIN member m on py.member_id = m.member_id;