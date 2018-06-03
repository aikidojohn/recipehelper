
create table recipe_index (
	id int not null auto_increment primary key,
    source varchar(64) not null,
    title tinytext not null,
    prep_time int not null,
    servings int not null,
    calories int,
    description text,
    link varchar(512),
    ethnicity varchar(64),
    fulltext title_idx (title),
    fulltext description_idx(description),
    index prep_time_idx(prep_time) using btree,
    index calories_idx(calories) using btree
) DEFAULT CHARSET=utf8;

create table recipe_ingredient (
	id int not null auto_increment primary key,
    recipe_id int not null,
    amount_numerator int not null,
    amount_denominator int not null,
    measurement varchar(32),
    ingredient_name varchar(255),
    ingredient_id int,
    fulltext ingredient_name_idx(ingredient_name),
    foreign key recipe_ingredient_recipe_index_fk (recipe_id) references recipe_index (id)
) DEFAULT CHARSET=utf8;

create table recipe_step (
	id int not null auto_increment primary key,
    recipe_id int not null,
    step int not null,
    title varchar(255) not null,
    detail text not null,
    fulltext detail_idx(detail),
    foreign key recipe_step_recipe_index_fk (recipe_id) references recipe_index (id)
) DEFAULT CHARSET=utf8;

drop table ingredient_alternative;
drop table ingredient_seasonality;
drop table ingredient_availability;
drop table ingredient;
create table ingredient (
	id int not null auto_increment primary key,
    name varchar(128) not null,
    type varchar(32),
    category varchar(32),
    unique index name_idx (name) using btree,
    index type_idx (type) using hash,
    index category_idx (category) using btree
) default charset=utf8;

create table ingredient_alternative (
	id int not null auto_increment primary key,
    ingredient_id int not null,
    type varchar(32) not null,
    name varchar(128) not null,
    index type_idx(type) using hash,
    index name_idx(name) using btree,
    foreign key ingredient_alternative_ingredient_fk (ingredient_id) references ingredient (id)
) default charset=utf8;

create table ingredient_seasonality (
	id int not null auto_increment primary key,
    ingredient_id int not null,
    month_bitmap bit(12) not null,
    index month_bitmap_idx(month_bitmap) using btree,
    foreign key ingredient_seasonality_ingredient_fk (ingredient_id) references ingredient (id)
) default charset=utf8;

create table ingredient_availability (
	id int not null auto_increment primary key,
    ingredient_id int not null,
    month_bitmap bit(12) not null,
    index month_bitmap_idx(month_bitmap) using btree,
    foreign key ingredient_availability_ingredient_fk (ingredient_id) references ingredient (id)
) default charset=utf8;

create table category_seasonality (
	id int not null auto_increment primary key,
    category varchar(32) not null,
    month_bitmap bit(12) not null,
    index month_bitmap_idx(month_bitmap) using btree,
	index category_idx(category) using btree
) default charset=utf8;


create table category_availability (
	id int not null auto_increment primary key,
    category varchar(32) not null,
    month_bitmap bit(12) not null,
    index month_bitmap_idx(month_bitmap) using btree,
	index category_idx(category) using btree
) default charset=utf8;
