const path = require('path');

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
        }
}
