import React, { Component } from 'react';
import { Form, Input, Button } from 'semantic-ui-react';
import Product from './Product.js';
import ProductList from './ProductList.js';
import Client from './Client.js';
import './SearchForm.css'; 
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

//example product list
const products = [
    {id:1, name:"McDonalds"},
    {id:2, name:"Wendy's"},
    {id:3, name:"Burger King"},
    {id:4, name:"Arby's"}
]

class SearchForm extends Component {
    constructor(props) {
        super(props)
        this.state = {
            searchBarValue:"",
            laraAspect1:0,
            laraAspect2:0,
            laraAspect3:0,
            laraAspect4:0,
            resultList:[]
        }

        this.onInputChange = this.onInputChange.bind(this);
        this.onFormSubmit = this.onFormSubmit.bind(this);
    }

    //updates search bar when typing
    onInputChange(event) {
        this.setState({
            searchBarValue:event.target.value
        });
    }

    //when search is pressed
    onFormSubmit(event) {
        event.preventDefault();
        var toSubmit = {};
        toSubmit.queryText = this.state.searchBarValue;
        
        //add in user preferences later
        Client.search(toSubmit, response => {
            this.setState({
                resultList: response
            });
        })
    }

    render() {
        return (
            <div>
                <div>
                    <form onSubmit={this.onFormSubmit}>
                        <input
                            placeholder="Enter your query..."
                            value={this.state.searchBarValue}
                            onChange={this.onInputChange}
                        />
                        <button type="submit">
                            Search
                        </button>
                    </form>
                </div>
                
                <div>
                    <ProductList products={products} />`
                </div>
            </div>
        );
    }
}

export default SearchForm;
