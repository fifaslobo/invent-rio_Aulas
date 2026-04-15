package com.starterkit.springboot.fornecedor;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/fornecedores")
public class FornecedorPageController {

    private final FornecedorService fornecedorService;

    @Value("${security.api-key.admin:}")
    private String adminKey;

    public FornecedorPageController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("titulo", "Gestao de fornecedores");
        model.addAttribute("fornecedores", fornecedorService.listAll());
        model.addAttribute("pageScript", "/js/fornecedores.js");
        return "fornecedores/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("titulo", "Novo Fornecedor");
        model.addAttribute("fornecedor", null);
        model.addAttribute("fornecedorForm", new FornecedorForm());
        model.addAttribute("modo", "novo");
        model.addAttribute("pageScript", "/js/fornecedores.js");
        return "fornecedores/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Fornecedor fornecedor = fornecedorService.getById(id);
        FornecedorForm form = new FornecedorForm();
        copyFornecedorToForm(fornecedor, form);
        model.addAttribute("titulo", "Editar Fornecedor");
        model.addAttribute("fornecedor", fornecedor);
        model.addAttribute("fornecedorForm", form);
        model.addAttribute("modo", "editar");
        model.addAttribute("pageScript", "/js/fornecedores.js");
        return "fornecedores/form";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        Fornecedor fornecedor = fornecedorService.getById(id);
        model.addAttribute("titulo", "Ficha do Fornecedor");
        model.addAttribute("fornecedor", fornecedor);
        model.addAttribute("pageScript", "/js/fornecedores.js");
        return "fornecedores/detalhe";
    }

    @GetMapping("/codigo/{codigo}")
    public String detalhePorCodigo(@PathVariable String codigo, Model model) {
        Fornecedor fornecedor = fornecedorService.getByCodigo(codigo);
        model.addAttribute("titulo", "Ficha do Fornecedor");
        model.addAttribute("fornecedor", fornecedor);
        model.addAttribute("pageScript", "/js/fornecedores.js");
        return "fornecedores/detalhe";
    }

    @GetMapping("/scan")
    public String scan(Model model) {
        model.addAttribute("titulo", "Ler QR Code");
        model.addAttribute("pageScript", "/js/fornecedores.js");
        return "fornecedores/scan";
    }

    @PostMapping
    public String criar(@Valid FornecedorForm fornecedorForm, BindingResult bindingResult, Model model) {
        if (!isValidAdminKey(fornecedorForm.getAdminApiKey())) {
            bindingResult.rejectValue("adminApiKey", "adminApiKey.invalid", "Chave de administrador invalida.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "Novo Fornecedor");
            model.addAttribute("fornecedor", null);
            model.addAttribute("modo", "novo");
            model.addAttribute("pageScript", "/js/fornecedores.js");
            return "fornecedores/form";
        }

        fornecedorService.create(fornecedorForm);
        return "redirect:/fornecedores?status=created";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid FornecedorForm fornecedorForm, BindingResult bindingResult,
            Model model) {
        Fornecedor fornecedor = fornecedorService.getById(id);
        if (!isValidAdminKey(fornecedorForm.getAdminApiKey())) {
            bindingResult.rejectValue("adminApiKey", "adminApiKey.invalid", "Chave de administrador invalida.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "Editar Fornecedores");
            model.addAttribute("modo", "editar");
            model.addAttribute("fornecedor", fornecedor);
            model.addAttribute("pageScript", "/js/fornecedores.js");
            return "fornecedores/form";
        }

        fornecedorService.update(id, fornecedorForm);
        return "redirect:/fornecedores?status=updated";
    }

    private boolean isValidAdminKey(String providedKey) {
        return providedKey != null && !providedKey.trim().isEmpty() && providedKey.equals(adminKey);
    }

    private void copyFornecedorToForm(Fornecedor fornecedor, FornecedorForm form) {
        form.setNome(fornecedor.getNome());
        form.setMorada(fornecedor.getMorada());
        form.setLocalidade(fornecedor.getLocalidade());
        form.setEmail(fornecedor.getEmail());
        form.setTelemovel(fornecedor.getTelemovel());
        form.setSite(fornecedor.getSite());
    }
}
