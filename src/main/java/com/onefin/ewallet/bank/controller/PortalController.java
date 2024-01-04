package com.onefin.ewallet.bank.controller;

import com.onefin.ewallet.bank.repository.jpa.VietinEfastTransactionViewRepo;
import com.onefin.ewallet.common.base.search.CustomRsqlVisitor;
import com.onefin.ewallet.common.domain.bank.vietin.VietinEfastTransactionView;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/inside/bank")
public class PortalController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PortalController.class);

	@Autowired
	private VietinEfastTransactionViewRepo vietinEfastTransactionViewRepo;

	@GetMapping("/vietin-virtual-acct-trans/list")
	public ResponseEntity<?> getVietinVirtualAccountTransList(@RequestParam(value = "search", required = false) String search, @PageableDefault(sort = {"onefinCreatedDate"}, direction = Sort.Direction.DESC) Pageable pageable, Principal user) {
		Page<VietinEfastTransactionView> results;
		if (search != null) {
			Node rootNode = new RSQLParser().parse(search);
			Specification<VietinEfastTransactionView> spec = rootNode.accept(new CustomRsqlVisitor<>());
			results = vietinEfastTransactionViewRepo.findAll(spec, pageable);
		} else {
			results = vietinEfastTransactionViewRepo.findAll(pageable);
		}
		return new ResponseEntity<>(results, HttpStatus.OK);

	}

	@GetMapping("/vietin-virtual-acct-trans/{id}")
	public ResponseEntity<?> getVietinVirtualAccountTransById(@PathVariable(value = "id") UUID id, Principal user) {
		return new ResponseEntity<>(vietinEfastTransactionViewRepo.findById(id), HttpStatus.OK);
	}


}
