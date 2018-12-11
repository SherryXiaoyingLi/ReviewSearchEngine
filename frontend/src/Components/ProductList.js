import React from "react";
import Product from "./Product.js";

function ProductList(props) {
    return (
        <div>
            {props.products.map(p => 
                <Product id={p.id} name={p.name} />
            )}
        </div>
    );
}

export default ProductList;
