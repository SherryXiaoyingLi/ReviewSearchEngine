import React from "react";
import "./Product.css";
import PropTypes from "prop-types";

function Product(props) {
    return (
        <div className="product">
            <span>{props.name}</span>
        </div>
    );
}

Product.propTypes = {
    name: PropTypes.string.isRequired
};

export default Product;
