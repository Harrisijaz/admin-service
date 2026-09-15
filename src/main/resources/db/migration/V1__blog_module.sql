create table if not exists blog_categories (
    id varchar(255) not null primary key,
    name varchar(100) not null,
    slug varchar(140) not null,
    description varchar(500),
    status varchar(20) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_blog_category_name unique (name),
    constraint uk_blog_category_slug unique (slug)
);

create table if not exists blog_tags (
    id varchar(255) not null primary key,
    name varchar(80) not null,
    slug varchar(120) not null,
    constraint uk_blog_tag_name unique (name),
    constraint uk_blog_tag_slug unique (slug)
);

create table if not exists blog_posts (
    id varchar(255) not null primary key,
    title varchar(150) not null,
    slug varchar(180) not null,
    excerpt varchar(300),
    meta_title varchar(60),
    meta_description varchar(300),
    cover_image_url varchar(1000),
    cover_image_alt varchar(180),
    author_name varchar(120),
    category_id varchar(255),
    estimated_read_time integer not null,
    status varchar(20) not null,
    published_date timestamp null,
    created_at timestamp not null,
    updated_at timestamp not null,
    created_by varchar(120) not null,
    updated_by varchar(120) not null,
    archived_at timestamp null,
    archived_by varchar(120),
    version bigint,
    constraint uk_blog_post_slug unique (slug),
    constraint fk_blog_post_category foreign key (category_id) references blog_categories(id)
);

create table if not exists blog_post_tags (
    post_id varchar(255) not null,
    tag_id varchar(255) not null,
    constraint uk_blog_post_tag unique (post_id, tag_id),
    constraint fk_blog_post_tags_post foreign key (post_id) references blog_posts(id),
    constraint fk_blog_post_tags_tag foreign key (tag_id) references blog_tags(id)
);

create table if not exists blog_content_blocks (
    id varchar(255) not null primary key,
    post_id varchar(255) not null,
    type varchar(40) not null,
    block_order integer not null,
    data_json text not null,
    constraint fk_blog_content_post foreign key (post_id) references blog_posts(id)
);

create table if not exists blog_product_links (
    id varchar(255) not null primary key,
    post_id varchar(255) not null,
    label varchar(120) not null,
    href varchar(500) not null,
    link_order integer not null,
    constraint fk_blog_product_link_post foreign key (post_id) references blog_posts(id)
);

create table if not exists blog_post_slug_history (
    id varchar(255) not null primary key,
    post_id varchar(255) not null,
    old_slug varchar(180) not null,
    created_at timestamp not null,
    constraint uk_blog_old_slug unique (old_slug),
    constraint fk_blog_slug_history_post foreign key (post_id) references blog_posts(id)
);

create index idx_blog_category_status on blog_categories(status);
create index idx_blog_post_status on blog_posts(status);
create index idx_blog_post_slug on blog_posts(slug);
create index idx_blog_post_published on blog_posts(published_date);
create index idx_blog_post_category on blog_posts(category_id);
create index idx_blog_post_created on blog_posts(created_at);
create index idx_blog_post_status_published on blog_posts(status, published_date);
create index idx_blog_content_post_order on blog_content_blocks(post_id, block_order);
create index idx_blog_product_link_post_order on blog_product_links(post_id, link_order);
create index idx_blog_slug_history_post on blog_post_slug_history(post_id);
