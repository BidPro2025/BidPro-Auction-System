package com.bidpro.service;
import com.bidpro.dao.*;
import com.bidpro.entities.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.EntityNotFoundException;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;
import javax.imageio.ImageIO;

@Service
public class FileUploadService {
    private final ProductImagesDao images;
    private final ProductService products;
    private final Path directory;
    public FileUploadService(ProductImagesDao images,ProductService products,@Value("${app.upload-dir:.data/uploads}") String directory) {
        this.images=images;this.products=products;this.directory=Path.of(directory).toAbsolutePath().normalize();
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String uploadProductImage(MultipartFile file,String productID) {
        var product=products.requireProductForUpdate(productID);products.requireOwner(product);
        if (product.getProductStatus()!=Product.ProductStatus.PENDING && product.getProductStatus()!=Product.ProductStatus.REJECTED)
            throw new IllegalArgumentException("Images can be changed only before approval");
        if (file==null || file.isEmpty() || file.getSize()>5*1024*1024) throw new IllegalArgumentException("Choose an image smaller than 5 MB");
        String content=file.getContentType();
        if (!"image/png".equals(content) && !"image/jpeg".equals(content)) throw new IllegalArgumentException("Only PNG and JPEG images are supported");
        try {
            // Decode and re-encode the image to reject disguised files and discard embedded metadata.
            var decoded=decodeImage(file);
            String extension="image/png".equals(content)?"png":"jpg";
            String filename=UUID.randomUUID()+"."+extension;
            Files.createDirectories(directory);Path destination=directory.resolve(filename).normalize();
            if (!destination.startsWith(directory)) throw new IllegalArgumentException("Invalid file path");
            if (!ImageIO.write(decoded,extension,destination.toFile())) throw new IllegalArgumentException("Unable to decode this image format");
            String url="/api/images/"+filename;
            try {images.saveAndFlush(new ProductImages(productID,url));}
            catch (RuntimeException error) {Files.deleteIfExists(destination);throw error;}
            return url;
        } catch (IOException error) {throw new IllegalStateException("Unable to store image",error);}
    }
    private java.awt.image.BufferedImage decodeImage(MultipartFile file) {
        try (var input=file.getInputStream();var stream=ImageIO.createImageInputStream(input)) {
            var readers=ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new IllegalArgumentException("The file is not a valid image");
            var reader=readers.next();
            try {
                reader.setInput(stream,true,true);
                long width=reader.getWidth(0),height=reader.getHeight(0);
                if (width<=0 || height<=0 || width*height>25_000_000L)
                    throw new IllegalArgumentException("Image dimensions are too large");
                return reader.read(0);
            } finally {reader.dispose();}
        } catch (IOException error) {throw new IllegalArgumentException("The file is not a valid image",error);}
    }
    public Resource load(String name) {
        if (!name.matches("[a-f0-9-]+\\.(png|jpg)")) throw new EntityNotFoundException("Image not found");
        var path=directory.resolve(name).normalize();
        if (!path.startsWith(directory) || !Files.isRegularFile(path)) throw new EntityNotFoundException("Image not found");
        try {return new UrlResource(path.toUri());}catch (IOException e) {throw new EntityNotFoundException("Image not found");}
    }
}
