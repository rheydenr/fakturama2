package com.sebulli.fakturama.views.datatable.tree.model;

public interface IModelVisitor {
	public void visitMovingBox(TreeParent box, Object passAlongArgument);
	public void visitBook(TreeObject book, Object passAlongArgument);
}
