package com.bidpro.config;
import com.bidpro.dao.*;
import com.bidpro.entities.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
/** Local examples only. Never enabled by the MySQL profile. Existing records are preserved. */
@Component
@Profile("local")
@ConditionalOnProperty(name="app.seed.enabled",havingValue="true",matchIfMissing=true)
public class LocalDataInitializer implements ApplicationRunner {
    private final UserDao users; private final CategoryDao categories; private final ProductDao products; private final AuctionDao auctions; private final PasswordEncoder passwords;
    public LocalDataInitializer(UserDao users, CategoryDao categories, ProductDao products, AuctionDao auctions, PasswordEncoder passwords) {
        this.users=users;this.categories=categories;this.products=products;this.auctions=auctions;this.passwords=passwords;
    }
    @Override @Transactional public void run(ApplicationArguments args) {
        User admin=account("admin", "Alex", "Morgan", User.Role.ADMIN);
        User seller=account("seller", "Jamie", "Taylor", User.Role.SELLER);
        account("bidder", "Sam", "Rivera", User.Role.BIDDER);
        Category electronics=category("Electronics","Cameras, audio and collectible technology",0);
        Category collectibles=category("Collectibles","Objects with a story worth keeping",1);
        Category home=category("Home & Living","Craftsmanship for everyday spaces",2);
        if(products.count()==0) {
            sample("Vintage film camera", "A carefully maintained 35mm camera with a standard lens and carrying case.", seller, admin, electronics, "2400", "100", -1, 48);
            sample("Mechanical collector watch", "Classic hand-wound timepiece with a leather strap. Excellent working condition.",seller,admin,collectibles,"4800","200",-2,72);
            sample("Handcrafted reading lamp", "Solid wood desk lamp with a warm adjustable light. Made by an independent studio.",seller,admin,home,"1800","50",24,96);
        }
    }
    private User account(String username,String first,String last,User.Role role) {
        return users.findByEmail(username+"@bidpro.local").orElseGet(()->users.save(new User(UUID.randomUUID().toString(),first,last,username,username+"@bidpro.local","",passwords.encode("BidPro123!"),role,User.Status.APPROVED)));
    }
    private Category category(String name,String description,int order) {
        Category found=categories.findByName(name);if(found!=null)return found;
        Category value=new Category();value.setCategoryID(UUID.randomUUID().toString());value.setName(name);value.setDescription(description);value.setVisibility(true);value.setDisplayOrder(order);return categories.save(value);
    }
    private void sample(String title,String description,User seller,User admin,Category category,String price,String gap,int startHours,int endHours) {
        LocalDateTime now=LocalDateTime.now();
        Product product=new Product(UUID.randomUUID().toString(),title,description,seller.getUserID(),category.getCategoryID(),null,Product.ProductStatus.APPROVED,
            now.plusHours(startHours),now.plusHours(endHours),new BigDecimal(price),new BigDecimal(gap),Product.ProductCondition.USED,Product.PromotionStatus.NONE);
        products.saveAndFlush(product);
        Auction auction=new Auction();auction.setAuctionID(UUID.randomUUID().toString());auction.setTitle(title);auction.setProductID(product.getProductID());
        auction.setStartDate(product.getAuctionStartDate());auction.setEndDate(product.getAuctionEndDate());auction.setStartingPrice(product.getStartingPrice());
        auction.setStatus(startHours<=0?Auction.Status.ONGOING:Auction.Status.UPCOMING);auction.setAuctionType(Auction.AuctionType.STANDARD);auction.setCreatedBy(admin.getUserID());
        auctions.saveAndFlush(auction);product.setAuctionID(auction.getAuctionID());products.save(product);
    }
}

