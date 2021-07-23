package com.fenoreste.rest.Dao;

import com.fenoreste.rest.Entidades.Productos;

public class ProductsDAO extends FacadeProductos<Productos> {

    public ProductsDAO() {
        super(Productos.class);
    }
}
