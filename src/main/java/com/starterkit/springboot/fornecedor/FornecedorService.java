package com.starterkit.springboot.fornecedor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.http.HttpStatus;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


@Service
public class FornecedorService {

    private final FornecedorRepository repo;
    private final Path fornecedoresUploadDir;

    public FornecedorService(FornecedorRepository repo, @Value("${app.upload-dir:./uploads}") String uploadDir) {
        this.repo = repo;
        this.fornecedoresUploadDir = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("fornecedor");
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(fornecedoresUploadDir);
        } catch (IOException ex) {
            throw new IllegalStateException("Nao foi possivel criar a pasta de uploads", ex);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void fillMissingCodigoUnico() {
        List<Fornecedor> fornecedores = repo.findAll();
        boolean changed = false;
        for (Fornecedor fornecedor : fornecedores) {
            if (!StringUtils.hasText(fornecedor.getCodigoUnico())) {
                fornecedor.setCodigoUnico(UUID.randomUUID().toString());
                changed = true;
            }
        }
        if (changed) {
            repo.saveAll(fornecedores);
        }
    }

    public List<Fornecedor> listAll() {
        return repo.findAll();
    }

    public Fornecedor getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor nao encontrado"));
    }

    public Fornecedor getByCodigo(String codigo) {
        return repo.findByCodigoUnico(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fornecedor nao encontrado"));
    }

    public Fornecedor create(FornecedorRequest request) {
        Fornecedor fornecedor = new Fornecedor();
        applyRequest(fornecedor, request);
        return repo.save(fornecedor);
    }

    public Fornecedor create(FornecedorForm form) {
        Fornecedor fornecedor = new Fornecedor();
        applyRequest(fornecedor, form);
        fornecedor.setImagemPath(storeImage(form.getImagem(), null));
        return repo.save(fornecedor);
    }

    public Fornecedor update(Long id, FornecedorRequest request) {
        Fornecedor fornecedor = getById(id);
        applyRequest(fornecedor, request);
        return repo.save(fornecedor);
    }

    public Fornecedor update(Long id, FornecedorForm form) {
        Fornecedor fornecedor = getById(id);
        applyRequest(fornecedor, form);                     
        fornecedor.setImagemPath(storeImage(form.getImagem(), fornecedor.getImagemPath()));
        return repo.save(fornecedor);
    }

    public void delete(Long id) {
        Fornecedor fornecedor = getById(id);
        deleteStoredImage(fornecedor.getImagemPath());
        repo.delete(fornecedor);
    }

    private void applyRequest(Fornecedor fornecedor, FornecedorRequest request) {
        fornecedor.setNome(request.getNome());
        fornecedor.setMorada(request.getMorada());
        fornecedor.setLocalidade(request.getLocalidade());
        fornecedor.setEmail(request.getEmail());
        fornecedor.setTelemovel(request.getTelemovel());
        fornecedor.setSite(request.getSite());
    }

    private String storeImage(MultipartFile imagem, String currentImagePath) {
        if (imagem == null || imagem.isEmpty()) {
            return currentImagePath;
        }

        String originalName = StringUtils.cleanPath(imagem.getOriginalFilename());
        String extension = getExtension(originalName);
        if (!isAllowedImageExtension(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de imagem nao suportado");
        }

        String generatedName = UUID.randomUUID().toString() + extension;
        Path destination = fornecedoresUploadDir.resolve(generatedName).normalize();
        if (!destination.startsWith(fornecedoresUploadDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome de ficheiro invalido");
        }

        try (InputStream inputStream = imagem.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao guardar a imagem");
        }

        deleteStoredImage(currentImagePath);
        return "fornecedores/" + generatedName;
    }

    private void deleteStoredImage(String imagePath) {
        if (!StringUtils.hasText(imagePath)) {
            return;
        }

        String relativePath = imagePath.replace('/', java.io.File.separatorChar);
        Path filePath = fornecedoresUploadDir.getParent().resolve(relativePath).normalize();
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao remover a imagem");
        }
    }

    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0) {
            return "";
        }
        return fileName.substring(index).toLowerCase(Locale.ROOT);
    }

    private boolean isAllowedImageExtension(String extension) {
        return ".png".equals(extension)
                || ".jpg".equals(extension)
                || ".jpeg".equals(extension)
                || ".webp".equals(extension)
                || ".gif".equals(extension);
    }
}
