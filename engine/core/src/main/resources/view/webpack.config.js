const path = require('path');
const UglifyJsPlugin = require('uglifyjs-webpack-plugin')

module.exports = {
        entry: './core/Drawer.js',
        output: {
                path: path.resolve(__dirname, 'bin'),
                filename: 'bundle.js',
                library: 'Drawer',
                libraryExport: 'Drawer'
        },
        module: {
                rules: [{
                        test: /\.js$/,
                        use: {
                                loader: 'babel-loader',
                                options: {
                                        "presets": [ "es2015", "stage-3" ]
                                }
                        }

                }]
        },
        plugins: [
                new UglifyJsPlugin()
        ]
}
